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
    from .models import Session, User, Post
    db.create_all()

    from app.database.helpers import load_data
    load_data(db)

from .routes.auth import auth_blueprint
app.register_blueprint(auth_blueprint)

from .routes.user import user_blueprint
app.register_blueprint(user_blueprint)
