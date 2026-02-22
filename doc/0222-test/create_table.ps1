$connString = "Server=localhost,47158;Database=sql-db;User Id=sa;Password=shrkfl;TrustServerCertificate=True;Connection Timeout=10"
$query = @"
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'table_01')
BEGIN
    CREATE TABLE table_01 (
        sys_id varchar(18) NOT NULL,
        att_id varchar(50) NOT NULL,
        grp_cd varchar(36) NULL,
        orgn_file_nm varchar(300) NULL,
        att_file_nm varchar(50) NULL,
        att_file_path varchar(500) NULL,
        att_file_siz int NULL,
        sort_ord int NULL,
        rem varchar(1000) NULL,
        sts char(1) NULL,
        reg_id varchar(50) NULL,
        reg_dt datetime2 NULL,
        mod_id varchar(50) NULL,
        mod_dt datetime2 NULL,
        CONSTRAINT table_01_pk PRIMARY KEY (sys_id, att_id)
    );
    SELECT 'Table created successfully' as Result;
END
ELSE
BEGIN
    SELECT 'Table already exists' as Result;
END
"@

try {
    Write-Output "Connecting to localhost,47158..."
    $conn = New-Object System.Data.SqlClient.SqlConnection($connString)
    $conn.Open()
    $cmd = $conn.CreateCommand()
    $cmd.CommandText = $query
    $result = $cmd.ExecuteScalar()
    Write-Output $result
    $conn.Close()
} catch {
    Write-Error $_.Exception.Message
    exit 1
}
