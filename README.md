
# Database Backup & Restore CLI Utility

## 📖 Overview
This project is a powerful and easy-to-use command-line tool (CLI) designed to help you perform **backups and restores of multiple databases**, including **SQL** and **MongoDB**, with options for encryption and compression for added security. Developed in Java, the utility automates the backup and restore process with a user-friendly interface.

## 🌟 Key Features
- **Supports SQL and MongoDB**: Backup and restore both SQL and NoSQL databases.
- **Encryption Option**: Secure your backups with an encryption AES key.
- **Automatic Compression**: Reduce the file size of your backups.
- **Progress Feedback**: Shows progress with a bar to track the operation.

## 🛠️ Project Setup
1. **Requirements**:
    - **Java 21** or higher installed.
    - CLI tools such as **Terminal** or **Command Prompt**.
2. **Clone the Repository**:
   ```bash
   git clone https://github.com/YourUsername/DatabaseBackupProject.git
   cd DatabaseBackupProject
   ```

## 🚀 Running the Project
### General Commands
1. **Backup**:
    - **SQL**:
      ```bash
      java -jar database-backup.jar --backup --type=sql --key=YOUR_KEY --entities=table1,table2
      ```
    - **MongoDB**:
      ```bash
      java -jar database-backup.jar --backup --type=mongo --key=YOUR_KEY --entities=collection1,collection2
      ```

2. **Restore**:
    - **SQL**:
      ```bash
      java -jar database-backup.jar --restore --type=sql --key=YOUR_KEY --file=my_backup.sql.gz
      ```
    - **MongoDB**:
      ```bash
      java -jar database-backup.jar --restore --type=mongo --key=YOUR_KEY --file=my_backup_mongo.json.gz
      ```

### Command Details
- `--backup`: Starts the backup process.
- `--restore`: Initiates data restoration.
- `--type`: Specifies the database type (`sql` or `mongo`).
- `--key`: Optional. Key for encrypting or decrypting data.
- `--entities`: Optional. List of specific tables or collections to include.
- `--file`: Specifies the backup file for restoration.

## 🔑 Backup Encryption
To enable encryption, pass a key with the `--key` parameter. This ensures that only someone with the key can restore the backup.

> **Important**: Keep your key safe! Without it, encrypted backups cannot be restored.

## ⚙️ Practical Example
Suppose you want to back up the `users` database and the `accounts` table in your SQL database:
```bash
java -jar database-backup.jar --backup --type=sql --entities=users,accounts --key=MY_SECURE_KEY
```

To restore, simply run:
```bash
java -jar database-backup.jar --restore --type=sql --file=backup.sql.gz --key=MY_SECURE_KEY
```

## 📂 Backup Directory Structure
Backups are saved in the `backups` folder in the user’s directory:
```
~/
└── backups/
    ├── sql/
    │   └── backup_yyyyMMdd_HHmmss.sql.gz
    └── mongo/
        └── backup_yyyyMMdd_HHmmss.json.gz
```

## 📞 Support and Contributions
Feel free to open an **Issue** or **Pull Request** on GitHub. We welcome collaboration and feedback!

## ⚠️ Disclaimer
This application performs critical database operations. I recommend conducting **tests in a secure environment** before using it in production.
