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

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.VariantDao;

import javax.persistence.Query;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class JPAVariantDao extends JPABaseDao<Variant, String> implements VariantDao {


    // stupid solution, if it will be time make it better
    public AndroidVariant find(String id)
    {
        return entityManager.find(AndroidVariant.class,id);
    }

    @Override
    public void delete(Variant variant) {
        Query q = createNativeQuery("{ $query : { variantID: '" + variant.getId() + "'} }");
        List<Installation> result = q.getResultList();

        if (!result.isEmpty()) {
            entityManager.remove(result.get(0));
        }
        /*entityManager.createQuery("delete from Installation i where i.variant in :variant")
                .setParameter("variant", variant).executeUpdate();*/
        Variant entity = find(variant.getId());
        super.delete(entity);
    }


    @Override
    public Variant findByVariantID(String variantID) {
        if (variantID == null)
            return null;

        String qS = String.format("db.variant.find({ 'variantID': '%s'})", variantID);

        List<Variant> result = entityManager.createNativeQuery(qS, Variant.class).getResultList();

        if (!result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
        //return (Variant) entityManager.createNativeQuery(qS).getSingleResult();

        // hibernate OGM doesnt support parameters  yet
        /*return getSingleResultForQuery(createQuery("select t from Variant t where t.variantID = :variantID")
                .setParameter("variantID", variantID));*/
    }

    @Override
    public boolean existsVariantIDForDeveloper(String variantID, String loginName) {

        String qS = String.format("db.variant.count({ '_id' : '%s' , 'developer' : '%s'})", variantID,loginName);
        Long numberOfVariants = (Long)entityManager.createNativeQuery(qS).getSingleResult();

        /*Long numberOfVariants = createQuery("select count(t) from Variant t where t.variantID = :variantID and t.developer = :developer", Long.class)
                .setParameter("variantID", variantID)
                .setParameter("developer", loginName).getSingleResult();

1        */
        return numberOfVariants == 1L;
    }


    @Override
    public List<String> findVariantIDsForDeveloper(String loginName) {
        String qS = String.format("db.variant.find( { 'developer' : '%s'}, {'id' : 1} )", loginName);
        return entityManager.createNativeQuery(qS).getResultList();
        /*return createQuery("select t.variantID from Variant t where t.developer = :developer", String.class)
                .setParameter("developer", loginName).getResultList(); */

    }

    @Override
    public List<Variant> findAllVariantsByIDs(List<String> variantIDs) {
        if (variantIDs.isEmpty()) {
            return Collections.emptyList();
        }

        StringBuilder sb = new StringBuilder();

        Iterator<String> i = variantIDs.iterator();

        while (i.hasNext())
        {
            sb.append("'").append(i.next()).append("'");

            if(i.hasNext())
            {
                sb.append(",");
            }
        }

        String sqlString = String.format("{ $query: { 'variantID' : { $in : [%s] } } }",sb.toString());

        Query q = createNativeQuery(sqlString);
        List<Variant> result = q.getResultList();


        return result;
        /*return createQuery("select t from Variant t where t.variantID IN :variantIDs")
                .setParameter("variantIDs", variantIDs).getResultList();*/

    }

    //Admin queries
    @Override
    public boolean existsVariantIDForAdmin(String variantID) {


        String sqlString = String.format("db.variant.count( { '_id' : '%s'})",variantID);
        Long numberOfVariants = (Long)entityManager.createNativeQuery(sqlString).getSingleResult();
        /*
        Long numberOfVariants = createQuery("select count(t) from Variant t where t.variantID = :variantID", Long.class)
                .setParameter("variantID", variantID).getSingleResult();
        */
        return numberOfVariants == 1L;
    }

    @Override
    public Class<Variant> getType() {
        return Variant.class;
    }
}
