const sql = require('mssql');

const config = {
    user: 'sa',
    password: 'shrkfl',
    server: '127.0.0.1',
    port: 47158,
    database: 'sql-db',
    options: {
        encrypt: false,
        trustServerCertificate: true
    }
};

async function testConnection() {
    try {
        console.log('Attempting to connect to MSSQL...');
        let pool = await sql.connect(config);
        console.log('Connected successfully!');
        
        console.log('Checking database name...');
        let result = await pool.request().query('SELECT DB_NAME() as db');
        console.log('Current Database:', result.recordset[0].db);
        
        await pool.close();
    } catch (err) {
        console.error('Connection failed:', err.message);
        if (err.originalError) {
          console.error('Original Error:', err.originalError.message);
        }
    }
}

testConnection();
