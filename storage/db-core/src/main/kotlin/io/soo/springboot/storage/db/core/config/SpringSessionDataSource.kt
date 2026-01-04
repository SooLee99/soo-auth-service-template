package io.soo.springboot.storage.db.core.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.session.jdbc.config.annotation.SpringSessionDataSource
import javax.sql.DataSource

@Bean
@SpringSessionDataSource
fun SpringSessionDataSource(@Qualifier("coreDataSource") ds: DataSource): DataSource = ds
