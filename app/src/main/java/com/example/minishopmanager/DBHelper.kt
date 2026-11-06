package com.example.minishopmanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "minishop.db"
        const val DB_VERSION = 2

        const val TABLE_PANIER = "panier"
        const val COL_ID = "id"
        const val COL_PRODUIT_ID = "produit_id"
        const val COL_NOM = "nom"
        const val COL_PRIX = "prix"
        const val COL_QUANTITE = "quantite"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_PANIER (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PRODUIT_ID INTEGER NOT NULL DEFAULT 0,
                $COL_NOM TEXT NOT NULL,
                $COL_PRIX REAL NOT NULL DEFAULT 0,
                $COL_QUANTITE INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migration v1 -> v2 : on ajoute les nouvelles colonnes sans perdre les données existantes
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_PANIER ADD COLUMN $COL_PRODUIT_ID INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE $TABLE_PANIER ADD COLUMN $COL_PRIX REAL NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE $TABLE_PANIER ADD COLUMN $COL_QUANTITE INTEGER NOT NULL DEFAULT 1")
        }
    }

    /**
     * Ajoute un produit au panier avec une quantité de 1.
     * Si le produit est déjà dans le panier, sa quantité est incrémentée de 1.
     */
    fun ajouterAuPanier(produitId: Int, nom: String, prix: Double) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val dejaPresent = db.rawQuery(
                "SELECT $COL_ID FROM $TABLE_PANIER WHERE $COL_PRODUIT_ID = ?",
                arrayOf(produitId.toString())
            ).use { it.moveToFirst() }

            if (dejaPresent) {
                db.execSQL(
                    "UPDATE $TABLE_PANIER SET $COL_QUANTITE = $COL_QUANTITE + 1 WHERE $COL_PRODUIT_ID = ?",
                    arrayOf(produitId)
                )
            } else {
                val values = ContentValues().apply {
                    put(COL_PRODUIT_ID, produitId)
                    put(COL_NOM, nom)
                    put(COL_PRIX, prix)
                    put(COL_QUANTITE, 1)
                }
                db.insert(TABLE_PANIER, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** Retourne tous les articles du panier, dans l'ordre d'ajout. */
    fun getPanier(): List<CartItem> {
        val items = mutableListOf<CartItem>()
        readableDatabase.query(
            TABLE_PANIER, null, null, null, null, null, "$COL_ID ASC"
        ).use { c ->
            val iProduit = c.getColumnIndexOrThrow(COL_PRODUIT_ID)
            val iNom = c.getColumnIndexOrThrow(COL_NOM)
            val iPrix = c.getColumnIndexOrThrow(COL_PRIX)
            val iQuantite = c.getColumnIndexOrThrow(COL_QUANTITE)
            while (c.moveToNext()) {
                items.add(
                    CartItem(
                        produitId = c.getInt(iProduit),
                        nom = c.getString(iNom) ?: "",
                        prix = c.getDouble(iPrix),
                        quantite = c.getInt(iQuantite)
                    )
                )
            }
        }
        return items
    }

    /** Supprime tous les articles du panier. */
    fun viderPanier() {
        writableDatabase.delete(TABLE_PANIER, null, null)
    }
}