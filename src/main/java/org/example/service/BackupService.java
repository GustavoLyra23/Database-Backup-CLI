package org.example.service;

public class BackupService {

    private static final BackupService instance = new BackupService(EncryptionService.getInstance());
    private EncryptionService encryptionService;

    private BackupService(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    public static BackupService getInstance() {
        return instance;
    }

    public void backup() {
        System.out.println("Backup is done");
    }


}
