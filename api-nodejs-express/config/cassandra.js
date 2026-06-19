const cassandra = require('cassandra-driver');
const { loadEnvFile } = require('node:process');
loadEnvFile();

const contactPoints = (process.env.CASSANDRA_CONTACT_POINTS || '127.0.0.1').split(',');
const port = parseInt(process.env.CASSANDRA_PORT, 10) || 9042;
const keyspace = process.env.CASSANDRA_KEYSPACE || 'keyspace';
const localDataCenter = process.env.CASSANDRA_LOCAL_DC || 'datacenter';

const authProvider = (process.env.CASSANDRA_USER && process.env.CASSANDRA_PASSWORD) 
    ? new cassandra.auth.PlainTextAuthProvider(process.env.CASSANDRA_USER, process.env.CASSANDRA_PASSWORD)
    : undefined;

const client = new cassandra.Client({
    contactPoints,
    localDataCenter,
    authProvider,
    protocolOptions: { port }
});

let initialized = false;

async function getSession() {
    if (!initialized) {
        await client.connect();
        
        await client.execute(`
            CREATE KEYSPACE IF NOT EXISTS ${keyspace}
            WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}
        `);

        await client.execute(`USE ${keyspace}`);

        await client.execute(`
            CREATE TABLE IF NOT EXISTS readings (
                device_id uuid,
                bucket_date text,
                ts bigint,
                sensor_values text,
                PRIMARY KEY ((device_id, bucket_date), ts)
            ) WITH CLUSTERING ORDER BY (ts DESC)
        `);

        initialized = true;
    }
    return client;
}

module.exports = { getSession };
