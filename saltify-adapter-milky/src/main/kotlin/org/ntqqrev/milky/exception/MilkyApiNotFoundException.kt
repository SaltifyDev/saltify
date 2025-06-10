package org.ntqqrev.milky.exception

class MilkyApiNotFoundException(apiName: String) : MilkyException(
    "API '$apiName' not found in the Milky service")