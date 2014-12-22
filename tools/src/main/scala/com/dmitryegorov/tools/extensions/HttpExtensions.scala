package com.dmitryegorov.tools.extensions

import com.ning.http.client._

object HttpExtensions {
    implicit class ResponseEx(val response: Response) extends AnyVal {
        def assertOk = {
            if (200 until 299 contains response.getStatusCode) {
                response
            }
            else {
                val code = response.getStatusCode
                val text = response.getStatusText
                val body = response.getResponseBody

                throw new RuntimeException(s"$code ($text): $body")
            }
        }
    }
}
