import csv
from pathlib import Path

from app.models import User, Post


def load_data(db):
    with open(Path(__file__).with_name("dummy_users.csv"), "r") as users_csv:
        reader = csv.reader(users_csv)
        next(reader, None)
        for user_row in reader:
            print(user_row)
            user = User(*user_row)
            db.session.add(user)

    with open(Path(__file__).with_name("dummy_posts.csv"), "r") as posts_csv:
        reader = csv.reader(posts_csv)
        next(reader, None)
        for post_row in reader:
            post_row[-1] = bool(int(post_row[-1])) # feo??
            post = Post(*post_row)
            db.session.add(post)
    db.session.commit()
