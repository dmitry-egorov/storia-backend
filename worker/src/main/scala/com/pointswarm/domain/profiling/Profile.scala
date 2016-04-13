package com.pointswarm.domain.profiling

import com.pointswarm.common.ProviderType
import ProviderType.ProviderType
import com.pointswarm.common.dtos.AccountId
import com.pointswarm.domain.common.ProfileIdAgg
import com.scalasourcing.model.Aggregate

object Profile extends Aggregate
{
    override type Id = ProfileIdAgg

    case class Create(accountId: AccountId, provider: ProviderType, providerData: Map[String, AnyRef]) extends Command

    case class Created(accountId: AccountId, provider: ProviderType, providerData: Map[String, AnyRef]) extends Event

    case object AlreadyExists extends Error

    case object NonExistingProfile extends State
    {
        def apply(event: Event) = event match
        {
            case Created(accountId, provider, providerData) => ExistingProfile(accountId, provider, providerData)
            case _ => this
        }

        def apply(command: Command) = command match
        {
            case Create(accountId, provider, providerData) => Created(accountId, provider, providerData)
        }
    }

    case class ExistingProfile(accountId: AccountId, provider: ProviderType, providerData: Map[String, AnyRef]) extends State
    {
        def apply(event: Event) = event match
        {
            case _ => this
        }

        def apply(command: Command) = command match
        {
            case Create(_,_,_) => AlreadyExists
        }
    }

    override def seed: State = NonExistingProfile
}
