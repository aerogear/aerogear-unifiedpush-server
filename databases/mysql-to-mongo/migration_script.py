#!/usr/bin/python

import MySQLdb
import pymongo
import datetime
from pymongo import MongoClient
import MySQLdb.cursors
mongo_client = None

def get_col_as_list(col):
    l = []
    for c in col:
        l.append(c.text)
    return l


def get_row_as_list(row):
    l = []
    for r in row:
        l.append(r.text)
    return l


def create_packet(colList, rowList):
    pack = {}
    for i in range(0, len(colL)):
        key = colList[i]
        if key == 'id':
            key = '_id'
        pack[key] = rowList[i]
    return pack


def store_packets(client, database_name, collection_name, packet_list, indexes=[]):

    db = client["unifiedpush"]

    foo = db[collection_name]
    foo.drop()

    #print packet_list

    if packet_list:
        foo.insert_many(packet_list)

    for i in indexes:
        foo.create_index([(i, pymongo.ASCENDING)])



def create_packet_list(colL, rows):
    packet_list = []
    for r in rows:
        pack = create_packet(colL, get_row_as_list(r))
        packet_list.append(pack)
    return packet_list




def move_db(mysql_server, mysql_user, mysql_password, mongo_server):
    conn = MySQLdb.connect(mysql_server, mysql_user, mysql_password, database, cursorclass=MySQLdb.cursors.DictCursor)
    mongo_client = MongoClient(mongo_server)
    cur = conn.cursor()
    # category
    collection = "category"
    cur.execute("SELECT * FROM " + collection)
    packet_list = cur.fetchall()
    for p in packet_list:
        p["_id"] = str(p["id"])
        del p["id"]
    store_packets(mongo_client,'%s' % database, collection, packet_list)
    # installation
    cur.execute("SELECT * FROM installation_category")
    installation_category = cur.fetchall()
    collection = "installation"
    cur.execute("SELECT * FROM " + collection)
    packet_list = cur.fetchall()
    for p in packet_list:
        p["_id"] = p["id"]
        del p["id"]

        if p["enabled"] == '\x01':
            p["enabled"] = True
        else:
            p["enabled"] = False

        p['categories'] = [ str(d['category_id']) for d in installation_category if d['installation_id'] == p["_id"]]
    store_packets(mongo_client,database, collection, packet_list)

    # variant
    collection = "variant"
    cur.execute("SELECT * FROM variant")
    variants = cur.fetchall()
    all_variants = []
    for v in variants:
        if v['VARIANT_TYPE'] == 'simplePush':
            cur.execute("SELECT * FROM simple_push_variant WHERE id = %s", (v["id"]))
            android_variants = cur.fetchall()

            for v2 in android_variants:
                # merge
                p = v.copy()
                p.update(v2)
                p["_id"] = v['VARIANT_TYPE'] + str(p["id"])
                del p["id"]
                p["type"] = int(p["type"])
                all_variants.append(p)
        else:
            cur.execute("SELECT * FROM " + v['VARIANT_TYPE'] + "_variant WHERE id = %s", (v["id"]))
            android_variants = cur.fetchall()

            for v2 in android_variants:
                # merge
                p = v.copy()
                p.update(v2)
                p["_id"] = v['VARIANT_TYPE'] + str(p["id"])
                del p["id"]
                if "production" in p:
                    if p["production"] == '\x01':
                        p["production"] = True
                    else:
                        p["production"] = False

                p["type"] = int(p["type"])
                all_variants.append(p)
    store_packets(mongo_client,database, collection, all_variants)
    # push_message_info ------------------------------------------------------------------------------------------------
    collection = "push_message_info"
    cur.execute("SELECT * FROM " + collection)
    packet_list = cur.fetchall()
    for p in packet_list:
        p["_id"] = p["id"]
        del p["id"]
        if p["served_variants"] is None:
            p["served_variants"] = 0

        if p["served_variants"] is None:
            p["served_variants"] = 0

        if p["app_open_counter"] is None:
            p["app_open_counter"] = 0L

        if p["total_variants"] is None:
            p["total_variants"] = 0

        if p["total_receivers"] is None:
            p["total_receivers"] = 0L



        cur.execute("SELECT id FROM variant_metric_info WHERE push_message_info_id = %s ", (p["_id"]))
        variant_infos = cur.fetchall()
        p["variantInformations"] = [ str(d['id']) for d in variant_infos]




    store_packets(mongo_client,database, collection, packet_list, ["submit_date", "total_receivers", "app_open_counter"])

    # variant_metric_info
    collection = "variant_metric_info"
    cur.execute("SELECT * FROM " + collection)
    packet_list = cur.fetchall()
    for p in packet_list:
        p["_id"] = p["id"]
        del p["id"]
        if p["delivery_status"] == '\x01':
            p["delivery_status"] = True
        else:
            p["delivery_status"] = False
        p["pushMessageInformation_id"] = p["push_message_info_id"]
        del p["push_message_info_id"]

        if p["served_batches"] is None:
            p["served_batches"] = 0

        if p["total_batches"] is None:
            p["total_batches"] = 0

        p["served_batches"] = int(p["served_batches"])
        p["total_batches"] = int(p["total_batches"])



    store_packets(mongo_client,database, collection, packet_list, ["variant_id"])
    # push_application
    collection = "push_application"
    cur.execute("SELECT * FROM " + collection)
    packet_list = cur.fetchall()
    for p in packet_list:
        cur.execute("SELECT id FROM variant WHERE api_key = %s",(p["id"]))
        app_variants = cur.fetchall()
        p["_id"] = p["id"]
        del p["id"]
        p["variants"] = [ str(d['id']) for d in app_variants]

    store_packets(mongo_client,database, collection, packet_list)

#-----------------------------------------------------------------------------------------

database = 'unifiedpush'
mysql_server = 'localhost'
mongo_server = 'localhost'
mysql_user = 'root'
mysql_password = 'root'

move_db(mysql_server, mysql_user, mysql_password, mongo_server)
