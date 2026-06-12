import os
from cassandra.cluster import Cluster
from cassandra.auth import PlainTextAuthProvider
from dotenv import load_dotenv

load_dotenv()

CASSANDRA_CONTACT_POINTS = os.getenv("CASSANDRA_CONTACT_POINTS", "127.0.0.1").split(",")
CASSANDRA_PORT = int(os.getenv("CASSANDRA_PORT", 9042))
CASSANDRA_KEYSPACE = os.getenv("CASSANDRA_KEYSPACE", "keyspace")
CASSANDRA_LOCAL_DC = os.getenv("CASSANDRA_LOCAL_DC", "datacenter")
CASSANDRA_USER = os.getenv("CASSANDRA_USER", "user")
CASSANDRA_PASSWORD = os.getenv("CASSANDRA_PASSWORD", "password")

auth_provider = None
if CASSANDRA_USER and CASSANDRA_PASSWORD:
    auth_provider = PlainTextAuthProvider(username=CASSANDRA_USER, password=CASSANDRA_PASSWORD)

cluster = Cluster(
    contact_points=CASSANDRA_CONTACT_POINTS,
    port=CASSANDRA_PORT,
    auth_provider=auth_provider,
)
session = cluster.connect()

# Ensure keyspace exists
session.execute(f"""
    CREATE KEYSPACE IF NOT EXISTS {CASSANDRA_KEYSPACE}
    WITH replication = {{'class': 'SimpleStrategy', 'replication_factor': 1}};
""")
session.set_keyspace(CASSANDRA_KEYSPACE)

def get_cassandra_session():
    return session
