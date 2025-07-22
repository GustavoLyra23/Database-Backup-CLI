package org.example.entities

class ConnectionEntity {
    @JvmField
    var dbType: String? = null

    @JvmField
    var url: String? = null

    @JvmField
    var user: String? = null

    @JvmField
    var password: String? = null

    @JvmField
    var dbName: String? = null

    constructor()

    constructor(dbType: String?, url: String?, user: String?, password: String?, dbName: String?) {
        this.dbType = dbType
        this.url = url
        this.user = user
        this.password = password
        this.dbName = dbName
    }

    class Builder {
        private var dbType: String? = null
        private var url: String? = null
        private var user: String? = null
        private var password: String? = null
        private var dbName: String? = null

        fun dbType(dbType: String?): Builder {
            this.dbType = dbType
            return this
        }

        fun url(url: String?): Builder {
            this.url = url
            return this
        }

        fun user(user: String?): Builder {
            this.user = user
            return this
        }

        fun password(password: String?): Builder {
            this.password = password
            return this
        }

        fun dbName(dbName: String?): Builder {
            this.dbName = dbName
            return this
        }

        fun build(): ConnectionEntity {
            return ConnectionEntity(dbType, url, user, password, dbName)
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}

