package com.boo.study.aggregate.user.domain

import com.boo.study.common.infrastructure.TableName
import com.boo.study.common.infrastructure.skeleton.BaseEntity
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = TableName.USER)
class UserEntity : BaseEntity<Long>(){

    lateinit var name : String

    lateinit var userId : String

    lateinit var password : String

    var age : Long = 1L

    lateinit var phoneNumber : String


}