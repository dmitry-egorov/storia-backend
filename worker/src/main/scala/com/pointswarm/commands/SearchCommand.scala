package com.pointswarm.commands

case class SearchCommand(query: String) {
    assert(query != null && query.trim.nonEmpty)
}
