package com.boo.study.aggregate.user.domain

import org.springframework.data.jpa.repository.JpaRepository

interface UserEntityRepository : JpaRepository<UserEntity, Long>{

}