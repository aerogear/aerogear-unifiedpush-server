/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.jpa.dao.impl;

import com.mongodb.*;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.dto.Count;
import org.jboss.aerogear.unifiedpush.jpa.MyMongoClient;

import javax.inject.Inject;
import java.net.UnknownHostException;
import java.util.*;

public class JPAPushApplicationDao extends JPABaseDao<PushApplication, String> implements PushApplicationDao {

    @Inject
    JPAInstallationDao installationDao;

    @Inject
    JPAVariantDao variantDao;

    @Override
    public void delete(PushApplication pushApplication) {
        PushApplication entity = entityManager.find(PushApplication.class, pushApplication.getId());
        List<Variant> variants = entity.getVariants();

        Iterator<Variant> i = variants.iterator();

        StringBuilder sb = new StringBuilder();
        while (i.hasNext())
        {
            sb.append("'").append(i.next().getId()) .append("'");
            if (i.hasNext())
            {
                sb.append(",");
            }
        }
        String qS = String.format("db.installation.find( { 'variant_id': { '$in' : [%s]} } )", sb.toString());
        List<Installation> installations= entityManager.createNativeQuery(qS, Installation.class).getResultList();

        for (Installation installation : installations)
        {
            installationDao.delete(installation);
            variantDao.delete(installation.getVariant());
        }

            /*entityManager.createQuery("delete from Installation i where i.variant in :variants")
                    .setParameter("variants", variants).executeUpdate();*/

        super.delete(entity);
    }

    @Override
    public PageResult<PushApplication, Count> findAllForDeveloper(String loginName, Integer page, Integer pageSize) {

        String qS = String.format("db.push_application.find({'developer': '%s'})",loginName);
        String qCS = String.format("db.push_application.count({'developer': '%s'})", loginName);

        Long count = (Long) entityManager.createNativeQuery(qCS).getSingleResult();

        List<PushApplication> entities = entityManager.createNativeQuery(qS, PushApplication.class)
                .setFirstResult(page * pageSize).setMaxResults(pageSize).getResultList();

        /*String select = "from PushApplication pa where pa.developer = :developer";

        Long count = entityManager.createQuery("select count(*) " + select, Long.class)
                .setParameter("developer", loginName).getSingleResult();
        List<PushApplication> entities = entityManager.createQuery("select pa " + select, PushApplication.class)
                .setFirstResult(page * pageSize).setMaxResults(pageSize)
                .setParameter("developer", loginName).getResultList();*/

        return new PageResult<PushApplication, Count>(entities, new Count(count));
    }


    @Override
    public List<String> findAllPushApplicationIDsForDeveloper (String loginName) {
        String qS = String.format("db.push_application.find( { 'developer' : '%s'}, {'api_key' : 1} )", loginName);
        List<Object []> objects = entityManager.createNativeQuery(qS).getResultList();
        List<String> api_keys =  new ArrayList<String>();
        for (Object [] o : objects)
        {
            api_keys.add((String) o[1]);
        }
        return api_keys;
        /*
        return createQuery("select pa.pushApplicationID from PushApplication pa where pa.developer = :developer", String.class)
                .setParameter("developer", loginName).getResultList();*/
    }

    @Override
    public PushApplication findByPushApplicationIDForDeveloper(String pushApplicationID, String loginName) {

        String qS = String.format("{ $query: { 'api_key' : '%s', 'developer': '%s'} }",pushApplicationID,loginName);
        return getSingleResultForQuery(createNativeQuery(qS));
        /*return getSingleResultForQuery(createQuery(
                "select pa from PushApplication pa where pa.pushApplicationID = :pushApplicationID and pa.developer = :developer")
                .setParameter("pushApplicationID", pushApplicationID)
                .setParameter("developer", loginName));*/
    }

    @Override
    public PushApplication findByPushApplicationID(String pushApplicationID) {
        String qS = String.format("{ $query: { 'api_key' : '%s'} }",pushApplicationID);
        return getSingleResultForQuery(createNativeQuery(qS));
        /*return getSingleResultForQuery(createQuery("select pa from PushApplication pa where pa.pushApplicationID = :pushApplicationID")
                .setParameter("pushApplicationID", pushApplicationID));*/
    }

    @Override
    public Map<String, Long> countInstallationsByType(String pushApplicationID) {

        // hibernate ogm doesnt support aggregation queries

        final List<Object[]> resultList = new ArrayList<Object[]>();
        final HashMap<String, Long> results = new HashMap<String, Long>();

        for (VariantType type : VariantType.values()) {
            results.put(type.getTypeName(), 0L);
        }


        String qS = String.format("db.push_application.find( {'api_key' : '%s'} )",pushApplicationID);
        PushApplication pa = (PushApplication) entityManager.createNativeQuery(qS, PushApplication.class).getSingleResult();


        DB db = null;
        try {
            db = MyMongoClient.getDB(entityManager);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        DBCollection installation = db.getCollection( "variant" );


        DBObject match  = BasicDBObjectBuilder.start().push("$match")
                .add("push_application_id", pa.getId()).get();
        DBObject group = BasicDBObjectBuilder.start().push("$group")
                .push("_id").add("variant_type","$VARIANT_TYPE").add("id", "$_id").pop()
                .push("sum").add("$sum", 1).get();



        AggregationOutput aggr = installation.aggregate(Arrays.asList(match, group));
        Iterator<DBObject> i = aggr.results().iterator();

        DBObject o;
        while (i.hasNext())
        {
            o = i.next();
            String variantString = (String) ((DBObject)o.get("_id")).get("variant_type");
            String id =  (String) ((DBObject)o.get("_id")).get("id");
            String qS2 = String.format("db.installation.count({'variant_id': '%s'})",id);
            Long l = (Long) entityManager.createNativeQuery(qS2).getSingleResult();
            Integer sum = (Integer) o.get("sum");


            VariantType variantType = null;
            Long value = l * sum;

            if (variantString.equals("simplePush") )
                variantType = VariantType.valueOf("SIMPLE_PUSH");
            else {
                variantType = VariantType.valueOf(variantString.toUpperCase());
            }

            results.put(variantType.getTypeName(), results.get(variantType.getTypeName()) + value);
            results.put(id, value);

        }

        /*
        final String jpql = "select v.type, v.variantID, count(*) from Installation i join i.variant v where i.variant.variantID in "
                + "(select v.variantID from PushApplication pa join pa.variants v where pa.pushApplicationID = :pushApplicationID) "
                + "group by v.type, v.variantID";

        final TypedQuery<Object[]> query = createQuery(jpql, Object[].class)
                .setParameter("pushApplicationID", pushApplicationID);
        final List<Object[]> resultList = query.getResultList();
        for (Object[] objects : resultList) {
            final Long value = (Long) objects[2];
            final VariantType variantType = (VariantType) objects[0];
            results.put(variantType.getTypeName(), results.get(variantType.getTypeName()) + value);
            results.put((String) objects[1], value);
        }*/

        return results;
    }

    @Override
    public long getNumberOfPushApplicationsForDeveloper(String name) {

        String qS = String.format("db.push_application.count({ 'developer' : '%s' })", name);
        return  (Long) entityManager.createNativeQuery(qS).getSingleResult();
        /*return createQuery("select count(pa) from PushApplication pa where pa.developer = :developer", Long.class)
                .setParameter("developer", name).getSingleResult();*/
    }

    @Override
    public List<PushApplication> findByVariantIds(List<String> variantIDs) {

        StringBuilder sb = new StringBuilder();

        Iterator<String> sI = variantIDs.iterator();
        while (sI.hasNext())
        {
            sb.append("'").append(sI.next()).append("'");

            if (sI.hasNext())
                sb.append(",");
        }

        String qS = String.format("db.push_application.find( { 'variants': { '$in' : [%s] } } )", sb.toString());
        return createNativeQuery(qS).getResultList();
        /*
        final String jpql = "select pa from PushApplication pa left join fetch pa.variants v where v.variantID in (:variantIDs)";

        return createQuery(jpql).setParameter("variantIDs", variantIDs).getResultList();*/
    }

    @Override
    public Class<PushApplication> getType() {
        return PushApplication.class;
    }

    //Specific queries to the Admin
    @Override
    public PageResult<PushApplication, Count> findAll(Integer page, Integer pageSize) {

        String qS = "db.push_application.find({})";
        String qCS = "db.push_application.count({})";

        Long count = (Long) entityManager.createNativeQuery(qCS).getSingleResult();

        List<PushApplication> entities = entityManager.createNativeQuery(qS, PushApplication.class)
                .setFirstResult(page * pageSize).setMaxResults(pageSize).getResultList();
        /*String select = "from PushApplication pa";

        Long count = entityManager.createQuery("select count(*) " + select, Long.class).getSingleResult();

        List<PushApplication> entities = entityManager.createQuery("select pa " + select, PushApplication.class)
                .setFirstResult(page * pageSize).setMaxResults(pageSize).getResultList();*/

        return new PageResult<PushApplication, Count>(entities, new Count(count));
    }

    @Override
    public PushApplication findAllByPushApplicationID(String pushApplicationID) {
        String qS = String.format("{ $query: { 'api_key' : '%s'} }",pushApplicationID);
        return getSingleResultForQuery(createNativeQuery(qS));
        /*return getSingleResultForQuery(createQuery(
                "select pa from PushApplication pa where pa.pushApplicationID = :pushApplicationID")
                .setParameter("pushApplicationID", pushApplicationID));*/
    }

    @Override
    public long getNumberOfPushApplicationsForDeveloper() {
        return  (Long) entityManager.createNativeQuery("db.push_application.count({})").getSingleResult();
        /*return createQuery("select count(pa) from PushApplication pa", Long.class)
                .getSingleResult();*/
    }

}
