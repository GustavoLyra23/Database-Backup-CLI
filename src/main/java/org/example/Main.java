package org.example;

import org.example.service.EncryptionService;
import org.example.util.RegexUtil;

import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        Scanner scanner = new Scanner(System.in);
        EncryptionService encryptionService = EncryptionService.getInstance();
        var command = scanner.nextLine();
        if (RegexUtil.isGenerateKey(command)) {
            System.out.println("Key: " + encryptionService.encodeKey(encryptionService.generateKey()));
        }

        if (RegexUtil.isDbParams(command)) {
            System.out.println("DB params are correct");
        } else {
            System.out.println("DB params are incorrect");
        }


    }


    }



