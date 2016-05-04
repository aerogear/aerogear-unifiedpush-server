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

import com.mongodb.BasicDBList;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.PushApplicationDao;
import org.jboss.aerogear.unifiedpush.dto.Count;

import java.util.*;

public class JPAPushApplicationDao extends JPABaseDao<PushApplication, String> implements PushApplicationDao {

    @Override
    public void delete(PushApplication pushApplication) {
        PushApplication entity = entityManager.find(PushApplication.class, pushApplication.getId());
        final List<Variant> variants = entity.getVariants();

        List<Installation> l;
        StringBuilder ids = new StringBuilder();
        Iterator<Variant> vI = variants.iterator();

        while (vI.hasNext())
        {
            ids.append("'").append(vI.next().getId()).append("'");

            if(vI.hasNext())
            {
                ids.append(",");
            }
        }


        if (!variants.isEmpty()) {
            String qS = String.format("{ $query : { 'variant_id' : { '$in' : [%s]} } }", ids.toString());
            l = entityManager.createNativeQuery(qS,Installation.class).getResultList();

            JPAInstallationDao idao = new JPAInstallationDao();
            for (Installation i : l)
            {
               entityManager.remove(i);
            }

            /*entityManager.createQuery("delete from Installation i where i.variant in :variants")
                    .setParameter("variants", variants).executeUpdate();*/
        }
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
        String qS = String.format("db.push_application.find( { 'developer' : '%s'}, {'id' : 1} )", loginName);
        List<String> ids =  entityManager.createNativeQuery(qS).getResultList();
        return ids;
        /*
        return createQuery("select pa.pushApplicationID from PushApplication pa where pa.developer = :developer", String.class)
                .setParameter("developer", loginName).getResultList();*/
    }

    @Override
    public PushApplication findByPushApplicationIDForDeveloper(String pushApplicationID, String loginName) {

        String qS = String.format("{ $query: { 'pushApplicationID' : '%s', 'loginName': '%s'} }",pushApplicationID,loginName);
        return getSingleResultForQuery(createNativeQuery(qS));
        /*return getSingleResultForQuery(createQuery(
                "select pa from PushApplication pa where pa.pushApplicationID = :pushApplicationID and pa.developer = :developer")
                .setParameter("pushApplicationID", pushApplicationID)
                .setParameter("developer", loginName));*/
    }

    @Override
    public PushApplication findByPushApplicationID(String pushApplicationID) {
        String qS = String.format("{ $query: { 'pushApplicationID' : '%s'} }",pushApplicationID);
        return getSingleResultForQuery(createNativeQuery(qS));
        /*return getSingleResultForQuery(createQuery("select pa from PushApplication pa where pa.pushApplicationID = :pushApplicationID")
                .setParameter("pushApplicationID", pushApplicationID));*/
    }

    @Override
    public Map<String, Long> countInstallationsByType(String pushApplicationID) {


        String qS = String.format("db.push_application.find( {'pushApplicationID' : '%s'}, {'variants' : 1 } )",pushApplicationID);
        Object [] pa = (Object[]) entityManager.createNativeQuery(qS).getSingleResult();

        BasicDBList variants = (BasicDBList) pa[1];


        StringBuilder sb = new StringBuilder();

        Iterator i = variants.iterator();
        while(i.hasNext())
        {
            sb.append("'").append(i.next()).append("'");
            if(i.hasNext())
            {
                sb.append(",");
            }
        }


        final HashMap<String, Long> results = new HashMap<String, Long>();

        for (VariantType type : VariantType.values()) {
            results.put(type.getTypeName(), 0L);
        }

        // main query
        String qSM = String.format("db.installation.aggreagate( " +
                "{ '$group' : {_id:'$province'}  } )", sb.toString());

        final List<Object[]> resultList = new ArrayList<Object[]>();

        for (Object[] objects : resultList) {
            final Long value = (Long) objects[2];
            final VariantType variantType = (VariantType) objects[0];
            results.put(variantType.getTypeName(), results.get(variantType.getTypeName()) + value);
            results.put((String) objects[1], value);
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

        String qS = String.format("{ $query : { 'variants' : { '$in' : [%s] } } }", sb.toString());
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
        String qS = String.format("{ $query: { 'pushApplicationID' : '%s'} }",pushApplicationID);
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
