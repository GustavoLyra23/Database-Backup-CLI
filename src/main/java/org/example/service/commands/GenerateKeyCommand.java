package org.example.service.commands;

import org.example.service.Command;
import org.example.util.EncryptionUtil;

import java.util.Objects;

public class GenerateKeyCommand implements Command {

    @Override
    public void execute(String command) {
        var key = EncryptionUtil.encodeKey(Objects.requireNonNull(EncryptionUtil.generateKey()));
        System.out.println("\uD83D\uDD11 Key: " + key);
    }
}