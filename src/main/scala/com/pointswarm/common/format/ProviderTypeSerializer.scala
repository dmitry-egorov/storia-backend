package com.pointswarm.common.format

import com.pointswarm.commands.ProviderType._
import com.pointswarm.commands._
import org.json4s._


class ProviderTypeSerializer
    extends CustomSerializer[ProviderType](format => (
        {
            case JString(value) => ProviderType.withName(value)
        },
        {
            case x: ProviderType => JString(x.toString)
        }
        ))
