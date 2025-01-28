# Database Backup & Restore CLI Utility

A command-line utility for performing database backups and restorations, supporting both SQL and MongoDB databases with encryption and compression capabilities.

## Overview

This Java-based CLI tool automates database backup and restore operations with built-in support for encryption, compression, and progress tracking. It provides a straightforward interface for managing database backups across different database types.

## Features

- Multi-database support (SQL and MongoDB)
- AES encryption for secure backups
- Automatic compression
- Real-time progress monitoring
- Flexible restoration options

## Prerequisites

- Java 21 or later
- Access to command-line interface
- Database connection credentials

## Installation

```bash
git clone https://github.com/YourUsername/DatabaseBackupProject.git
cd DatabaseBackupProject
```

## Usage

### Key Management

Generate a new encryption key:
```bash
--generate key
```

### Database Configuration

Set database connection parameters:
```bash
--db <dbType> --url <url> [--password <password>] [--user <user>] [--dbName <database name>]
```

### Backup Operations

Create a new backup:
```bash
--do backup [--entity [entity1, entity2]] [--key <encryption key>]
```

### Restore Operations

Restore from backup:
```bash
--restore --foldertypedb <mongo/sql> --folderName <folder name> [--saves [save1, save2]] [--key <encryption key>]
```

### Backup Management

List available backups:
```bash
--list [mongo/sql] [--folder <folder name>]
```

View available commands:
```bash
--help
```

## Security

The utility uses AES encryption for backup security. When using encryption:

- Store encryption keys securely
- Required for both backup and restore operations
- Cannot recover encrypted backups without the original key

## Example Workflow

1. Configure database connection:
```bash
--db sql --url jdbc:mysql://localhost:3306 --user admin --password securepass --dbName production
```

2. Create encrypted backup:
```bash
--do backup --entity [users, accounts] --key MY_SECURE_KEY
```

3. Restore specific tables:
```bash
--restore --foldertypedb sql --folderName backup_20250128 --key MY_SECURE_KEY
```

## Backup Storage

Backups are automatically organized in the user's home directory:

```
~/backups/
├── sql/
│   └── backup_yyyyMMdd_HHmmss.sql.gz
└── mongo/
    └── backup_yyyyMMdd_HHmmss.json.gz
```

## Contributing

We welcome contributions to improve the utility. Please submit issues and pull requests through our GitHub repository.

## Security Notice

This tool performs critical database operations. Always test thoroughly in a non-production environment before using in production systems.
