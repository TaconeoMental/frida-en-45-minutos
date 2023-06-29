import os

from flask import Flask
from flask_bcrypt import Bcrypt
from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate

app = Flask(__name__)

app_settings = os.getenv(
    'APP_SETTINGS',
    'app.config.DevelopmentConfig'
)
app.config.from_object(app_settings)

db = SQLAlchemy(app)
bcrypt = Bcrypt(app)
migrate = Migrate(app, db)

with app.app_context():
    from app.database.models import Session, User
    db.create_all()

    from app.database.populate_users import load_data
    load_data(db)

from .auth.routes import main as main_blueprint
app.register_blueprint(main_blueprint)
