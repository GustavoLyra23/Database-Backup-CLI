
# Database Backup & Restore CLI Utility

## 📖 Overview
This project is a powerful and easy-to-use command-line tool (CLI) designed to help you perform **backups and restores of multiple databases**, including **SQL** and **MongoDB**, with options for encryption and compression for added security. Developed in Java, the utility automates the backup and restore process with a user-friendly interface.

## 🌟 Key Features
- **Supports SQL and MongoDB**: Backup and restore both SQL and NoSQL databases.
- **Encryption Option**: Secure your backups with an encryption key(AES).
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

1. **Generate Encryption Key**:
   ```bash
   --generate key
   ```

2. **Set Database Parameters**:
   ```bash
   --db <dbType> --url <url> [--password <password>] [--user <user>] [--dbName <database name>]
   ```

3. **Backup**:
   ```bash
   --do backup [--entity [entity1, entity2]] [--key <encryption key>]
   ```

4. **Restore**:
   ```bash
   --restore --foldertypedb <mongo/sql> --folderName <folder name> [--saves [save1, save2]] [--key <encryption key>]
   ```

5. **List Backups**:
   ```bash
   --list [mongo/sql] [--folder <folder name>]
   ```

### Command Details

- `--generate key`: Generates and displays an encryption key.
- `--db`: Specifies database parameters.
- `--do backup`: Starts the backup process.
- `--restore`: Initiates data restoration with database type, folder name, and optional saves or key.
- `--list`: Lists available backups, optionally filtered by database type and folder.
- `--help`: Lists all the available commands.
## 🔑 Backup Encryption
To enable encryption, pass a key with the `--key` parameter. This ensures that only someone with the key can restore the backup.

> **Important**: Keep your key safe! Without it, encrypted backups cannot be restored.

## ⚙️ Practical Example

Suppose you want to back up the `users` and `accounts` tables in your SQL database with encryption:
```bash
--do backup --entity [users, accounts] --key MY_SECURE_KEY
```

To restore, simply run:
```bash
--restore --foldertypedb sql --folderName my_backup_folder --key MY_SECURE_KEY
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
