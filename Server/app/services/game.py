import json
import os
import random

import settings
from services import db


# TODO: Make auto restart find for game, where some players don`t check room

def get_or_create_room(user_id: int) -> int:
    # TODO: Add check for user, that already has room
    buffer = db.get_new_of_free_room(user_id)
    db.close_now_connection()
    return buffer


def check_game_room_for_user(room_id: int, user_id: int) -> str:
    buffer = db.check_game_room_for_user(room_id, user_id)
    if buffer == 'WAITING':
        if check_room_for_start_possibility(room_id):
            start_checked_for_game_game(room_id)
            buffer = 'STARTING'
    if buffer == 'STARTING':
        start_checked_for_game_game(room_id)
        if not db.game_room_set_user_checked(room_id, user_id):
            return ''
    elif buffer == 'WAITING_CHECK':
        db.game_room_set_user_checked(room_id, user_id)
        buffer = db.check_room_for_freeze(room_id)
        if buffer == 'WAITING_CHECK':
            db.close_now_connection()
            return buffer
        elif buffer == "CHECK_END":
            buffer = db.check_game_room_for_user(room_id, user_id)
            if buffer != 'STARTING' and check_room_for_start_possibility(room_id):
                buffer = 'STARTING'
            if buffer == 'STARTING':
                start_game(room_id)
    db.close_now_connection()
    return buffer


def check_room_for_start_possibility(room_id: int) -> bool:
    time = db.get_waiting_time_of_room(room_id)
    players = db.get_players_of_room(room_id)

    if time >= settings.ROOM_WAITING_TIME_FOR_SMALL_START and players >= settings.ROOM_PLAYERS_FOR_SMALL_START:
        return True
    return False


def get_remaining_time(room_id: int) -> int:
    buffer = db.get_remaining_time(room_id)
    db.close_now_connection()
    return buffer


def create_private_room(user_id: int) -> tuple[int, str]:
    token = generate_private_room_token()
    room_id = db.create_private_room(user_id, token)
    db.close_now_connection()
    return room_id, token


def generate_private_room_token() -> str:
    token = ''
    for i in range(settings.PRIVATE_ROOM_TOKEN_LEN):
        token += random.choice(settings.TOKEN_SYMBOLS)
    return token


def get_private_room(user_id: int, token: str) -> int:
    room_id = db.get_private_room(user_id, token)
    db.close_now_connection()
    return room_id


def get_users_in_private_room(room_id: int) -> list[str]:
    data = db.get_users_in_private_room(room_id)
    db.close_now_connection()
    return data


def start_private_room(room_id: int, user_id: int) -> None:
    room_owner = db.get_private_room_owner(room_id)
    if room_owner != user_id:
        return
    start_game(room_id)


def start_checked_for_game_game(room_id: int) -> None:
    db.start_checked_game(room_id)


def check_game_for_freeze_users(room_id: int) -> None:
    if db.is_room_started(room_id) != 1:
        return

    if not db.is_room_checked_time_end(room_id):
        return

    db.clean_room_for_freeze(room_id)


def update_checked_for_game(room_id: int) -> None:
    db.check_game_room(room_id)


def check_game_check_user_state(room_id: int) -> bool:
    return db.check_game_check_user_state(room_id)


def start_game(room_id: int) -> None:
    db.set_drawer(room_id)
    db.set_room_starting_status(room_id)
    db.auto_set_room_word(room_id)
    db.start_checked_started_room(room_id)


def get_role(room_id: int, user_id: int) -> str:
    painter_id = db.get_now_painter(room_id)
    db.close_now_connection()
    if user_id == painter_id:
        return 'PAINTER'
    else:
        return 'USER'


def get_messages(room_id: int) -> list[str]:
    data = db.get_messages_of_game(room_id)
    db.close_now_connection()
    return data


def update_wait_state(room_id: int) -> int:
    if db.is_room_waiting_state_end(room_id):
        buffer = next_drawer(room_id)
        db.set_room_waiting_state(room_id, False)
        if buffer:
            return -1
        return 2
    return 1


def start_wait_state(room_id: int) -> None:
    db.set_room_waiting_state(room_id, True)


def next_drawer(room_id: int) -> bool:
    if db.is_painter_last(room_id):
        db.stop_room(room_id)
        return True

    db.next_painter(room_id)
    db.auto_set_room_word(room_id)
    db.close_now_connection()
    return False


def try_variant(variant: str, room_id: int, user_id: int) -> bool:
    buffer = db.check_variant(variant, room_id)
    db.send_message(variant.lower().strip(), room_id)
    if buffer:
        word = db.get_room_word(room_id)
        name = db.get_user_name_by_id(user_id)
        db.set_room_status_message(json.dumps({"message": "Слово угадано!\nУгадал: " + name, "right_answer": word}), room_id)
        start_wait_state(room_id)
        db.auto_set_room_word(room_id)
    db.close_now_connection()
    return buffer


def get_status(room_id: int, user_id: int) -> int:
    buffer = db.is_room_started(room_id)
    db.set_user_checked_for_room(room_id, user_id)
    check_game_for_freeze_users(room_id)
    if buffer == 1:
        is_waiting = db.is_not_room_freeze(room_id)
        if is_waiting == 0:
            waiting_state = update_wait_state(room_id)
            if waiting_state == -1:
                db.close_now_connection()
                return -1
            elif waiting_state == 1:
                db.close_now_connection()
                return 1
            else:
                db.close_now_connection()
                return 2
        buffer += 1
    db.close_now_connection()
    if buffer == 2:
        if check_for_end_time(room_id):
            return -1
    return buffer


def get_room_status_message(room_id: int) -> str:
    message = db.get_room_status_message(room_id)
    db.close_now_connection()
    return message


def check_for_end_time(room_id: int) -> bool:
    buffer = db.is_time_end_in_room(room_id)
    if buffer:
        db.set_room_status_message(json.dumps({"message": "Время закончилось!", "right_answer": db.get_room_word(room_id)}), room_id)
        if db.is_painter_last(room_id):
            db.set_room_waiting_state(room_id, True)
            return True
        start_wait_state(room_id)
        return False
    return False


def get_now_painter(room_id: int) -> int:
    buffer = db.get_now_painter(room_id)
    db.close_now_connection()
    return buffer


def get_word(room_id: int, user_id: int) -> str:
    if get_now_painter(room_id) != user_id:
        return ''
    buffer = db.get_room_word(room_id)
    db.close_now_connection()
    return buffer


def send_canvas(room_id: int, user_id: int, canvas) -> None:
    if db.get_now_painter(room_id) != user_id:
        return

    with open(os.path.join(settings.UPLOAD_FOLDER, str(room_id) + '.png'), 'bw') as file:
        file.write(canvas)

    db.close_now_connection()
