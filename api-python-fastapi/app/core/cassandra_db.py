from cassandra.cluster import Cluster
from cassandra.auth import PlainTextAuthProvider
from app.config import settings

class CassandraManager:
    def __init__(self):
        self.cluster = None
        self.session = None

    def connect(self):
        auth_provider = None
        if settings.CASSANDRA_USER and settings.CASSANDRA_PASSWORD:
            auth_provider = PlainTextAuthProvider(
                username=settings.CASSANDRA_USER, 
                password=settings.CASSANDRA_PASSWORD
            )

        self.cluster = Cluster(
            contact_points=settings.CASSANDRA_CONTACT_POINTS,
            port=settings.CASSANDRA_PORT,
            auth_provider=auth_provider,
        )
        self.session = self.cluster.connect()
        
        # Ensure keyspace setup is isolated from module import side-effects
        self.session.execute(f"""
            CREATE KEYSPACE IF NOT EXISTS {settings.CASSANDRA_KEYSPACE}
            WITH replication = {{'class': 'SimpleStrategy', 'replication_factor': 1}};
        """)
        self.session.set_keyspace(settings.CASSANDRA_KEYSPACE)
        return self.session

    def close(self):
        if self.cluster:
            self.cluster.shutdown()

cassandra_db = CassandraManager()
