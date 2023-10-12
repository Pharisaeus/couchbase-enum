package org.example.configuration;

import org.example.repository.MyCouchbaseRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;

@Configuration
@EnableCouchbaseRepositories(basePackageClasses = {MyCouchbaseRepository.class})
public class BeansConfiguration {
}
