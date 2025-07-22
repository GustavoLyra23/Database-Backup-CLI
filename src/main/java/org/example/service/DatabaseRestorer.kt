package org.example.service

import org.example.entities.ConnectionEntity

interface DatabaseRestorer {
    fun restoreDatabase(
        key: String?,
        saves: MutableList<String?>?,
        fileDbType: String?,
        fileName: String?,
        connectionEntity: ConnectionEntity?
    )
}
