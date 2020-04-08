import threading
from queue import Queue
from spiderman import Spiderman
from domain import *
from general import *

# считываю из командной строки название проекта и url домашней страницы
NAME_OF_PROJECT = input('NAME OF PROJECT: ')  # 'TestProject'
HOMEPAGE_URL = input('HOMEPAGE URL: ')  # 'http://neprivet.ru/'
# получаем доменное имя
DOMAIN_NAME = get_domain_name(HOMEPAGE_URL)
QUEUE_FILE = NAME_OF_PROJECT + '/queue.txt'
CRAWLED_FILE = NAME_OF_PROJECT + '/crawled.txt'
# количество потоков (пауков)
SPIDERS_NUMBER = 8
thread_queue = Queue()
# Первый паучок
Spiderman(NAME_OF_PROJECT, HOMEPAGE_URL, DOMAIN_NAME)


# создаём потоки (все задачи сами распределены по потокам)
# все пауки помрут, когда остановим программу
def create_spiders():
    for spider_num in range(SPIDERS_NUMBER):
        spider = threading.Thread(target=task)
        spider.daemon = True
        spider.start()


# Основная задача - сделать следующую задачув очереди
def task():
    while True:
        url = thread_queue.get()
        Spiderman.crawling(threading.current_thread().name, url)
        thread_queue.task_done()


# Каждая ссылка в очереди - задание паукам/
# Функция создания заданий
def create_tasks():
    # помещаю все ссылки в очередь потоков
    for link in create_set(QUEUE_FILE):
        thread_queue.put(link)
    thread_queue.join()
    crawler()


# Проверяем, есть ли ссылки в очереди (queue-файл), и если есть, то обползает их
def crawler():
    # сперва конвертируем ссылки в множество
    queued = create_set(QUEUE_FILE)
    # если очередь не пустая, то ползаем
    if len(queued) > 0:
        # уведомляю в командной строке, сколько ссылок осталось ещё в очереди
        print('There are ' + str(len(queued)) + ' links more (in the queue)')
        create_tasks()


create_spiders()
crawler()
