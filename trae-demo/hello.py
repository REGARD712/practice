def get_current_time ():
    import datetime
    return datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

print(get_current_time())