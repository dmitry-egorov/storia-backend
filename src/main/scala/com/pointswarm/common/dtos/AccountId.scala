package com.pointswarm.common.dtos

case class AccountId(value: String) extends AnyVal
{
    override def toString = value
}

object AccountId
{
    implicit def fromString(s: String): AccountId = new AccountId(s)
    implicit def toString(id: AccountId): String = id.value
}