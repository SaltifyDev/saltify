package org.ntqqrev.saltify.exception

class CommandNotFoundException(val commandName: String) :
    Exception(
        "Command '$commandName' not found"
    )