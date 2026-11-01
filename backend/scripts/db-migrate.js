/**
 * Chạy các file database/migrations/*.sql theo thứ tự tên.
 * Mỗi migration phải idempotent (CREATE TABLE IF NOT EXISTS ...) nên chạy lại không sao.
 * KHÔNG xoá dữ liệu -> an toàn để chạy trên VPS mỗi lần deploy (deploy.sh tự gọi).
 *   npm run db:migrate
 */
const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');
const env = require('../src/config/env');

async function main() {
  const dir = path.join(__dirname, '..', 'database', 'migrations');
  const files = fs.existsSync(dir) ? fs.readdirSync(dir).filter((f) => f.endsWith('.sql')).sort() : [];

  const conn = await mysql.createConnection({ ...env.db, multipleStatements: true, charset: 'utf8mb4' });
  for (const file of files) {
    await conn.query(fs.readFileSync(path.join(dir, file), 'utf8'));
    console.log(`[db:migrate] ✓ ${file}`);
  }
  await conn.end();
  console.log(`[db:migrate] Xong (${files.length} migration)`);
}

main().catch((err) => {
  console.error('[db:migrate] Lỗi:', err.message);
  process.exit(1);
});
