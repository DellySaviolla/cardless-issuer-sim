# Cardless Issuer Simulator

Simulator Issuer untuk transaksi Cardless Withdrawal (REST API) berdasarkan spesifikasi FSD v1.0.8.

## Fitur
- **REST API Endpoint**: `/jalin/openapi/cardless/withdrawaloffus/debit` dan `/reversal`.
- **Web Dashboard**: Memonitor transaksi *real-time* (Auto-refresh) dan melihat detail JSON Request/Response.
- **Global Response Settings**: Mengubah RC, ProcCode, Amount, dan Company Code secara dinamis tanpa restart aplikasi.
- **Idempotency**: Mencegah *double debit* untuk request dengan STAN/RRN yang sama.

## Cara Menjalankan
1. Pastikan Java 17 dan Maven sudah terinstal.
2. Jalankan `mvn clean spring-boot:run`.
3. Buka dashboard di `http://localhost:8080`.
4. Gunakan Postman Collection (tersedia di repo) untuk melakukan *hit* API.

## Skenario Testing (Response Code)
| Kondisi | RC |
|---|---|
| Sukses / Approved | 00 |
| Account Blocked | 05 |
| Currency Salah | 12 |
| PAN Tidak Ditemukan | 14 |
| Saldo Kurang | 51 |
| PAN Expired | 54 |
| Melebihi Limit | 61 |

## Catatan Keamanan
- PAN dimasking di log UI (`084900******6789`).
- Secret key untuk *signature* diambil dari *environment variable* `JALIN_SECRET_KEY`.