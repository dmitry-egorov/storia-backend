package com.pointswarm.minions.registrator

import com.pointswarm.common.dtos._

class AccountAlreadyExistsException(accountId: AccountId) extends RuntimeException(s"Account $accountId already exists")
