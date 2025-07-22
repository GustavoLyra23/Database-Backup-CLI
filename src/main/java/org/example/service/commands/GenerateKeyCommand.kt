package org.example.service.commands

import org.example.service.Command
import org.example.util.EncryptionUtil
import java.util.*
import javax.crypto.SecretKey

class GenerateKeyCommand : Command {
    override fun execute(command: String?) {
        val key = EncryptionUtil.encodeKey(Objects.requireNonNull<SecretKey?>(EncryptionUtil.generateKey()))
        println("\uD83D\uDD11 Key: $key")
    }
}