package org.example.repository;

import org.example.model.SomeDocument;
import org.example.model.SomeEnum;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.Query;
import org.springframework.data.couchbase.repository.Scope;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static org.example.repository.Constants.COLLECTION;
import static org.example.repository.Constants.SCOPE;

@Scope(SCOPE)
@org.springframework.data.couchbase.repository.Collection(COLLECTION)
@Repository("myRepository")
public interface MyCouchbaseRepository extends CouchbaseRepository<SomeDocument, String> {
    List<SomeDocument> findByStatus(SomeEnum status, Pageable pageable);

    @Query("UPDATE #{#n1ql.collection} SET status = $newStatus WHERE #{#n1ql.filter} AND value in $values #{#n1ql.returning}")
    List<SomeDocument> changeStatus(@Param("values") Collection<String> values, @Param("newStatus") SomeEnum newStatus);
}
