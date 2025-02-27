package vnpt.trust_call.handler.exceptions

class WrongTimeInitializationException(
    override val message: String = "You should initialize RuntimePermissionRequester in Activity.onCreate method!",
) : Exception(message)