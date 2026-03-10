package ru.bonch.pivder.app.exception.notfound.impl

import ru.bonch.pivder.app.exception.notfound.NotFoundException

class TokenNotFoundException(
    override val message: String
) : NotFoundException(message)