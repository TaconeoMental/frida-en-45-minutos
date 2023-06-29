import os

basedir = os.path.abspath(os.path.dirname(__file__))

database_name = 'database/frida_45_min.db'
DATABASE_URI = f"sqlite:///{os.path.join(basedir, database_name)}"

class BaseConfig:
    SECRET_KEY = os.getenv('SECRET_KEY', os.urandom(24))
    DEBUG = False
    BCRYPT_LOG_ROUNDS = 13
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    SQLALCHEMY_DATABASE_URI = DATABASE_URI

class DevelopmentConfig(BaseConfig):
    DEBUG = True
    BCRYPT_LOG_ROUNDS = 4
