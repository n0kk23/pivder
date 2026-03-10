package ru.bonch.pivder.app.exception.unauthorized.impl

import ru.bonch.pivder.app.exception.unauthorized.UnauthorizedException

class InvalidTokenException(
    override val message: String
) : UnauthorizedException(message)