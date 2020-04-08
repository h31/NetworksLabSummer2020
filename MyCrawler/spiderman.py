from urllib.request import urlopen
from find_links import FinderLinks
from general import *
from domain import *


# Берёт HTML-ки из листа ожидания (очереди)
# и "обползает"
class Spiderman:

    # может быть куча пауков, но у них всех одна общая очередь
    # (лист ожидания) и один общий crawled
    name_of_project = ''
    homepage_url = ''
    domain_name = ''
    queue_name = ''
    crawled_name = ''
    queue = set()
    crawled = set()

    def __init__(self, name_of_project, homepage_url, domain_name):
        Spiderman.name_of_project = name_of_project
        Spiderman.homepage_url = homepage_url
        Spiderman.domain_name = domain_name
        Spiderman.queue_name = Spiderman.name_of_project + '/queue.txt'
        Spiderman.crawled_name = Spiderman.name_of_project + '/crawled.txt'
        self.boot()
        # Сначала есть только 1 страница - домашняя, нет смысла
        # заставлять кучу пауков ее смотреть, поэтому их зовём позже
        self.crawling('1st spider', Spiderman.homepage_url)

    # работа для первого паучка
    @staticmethod
    def boot():
        create_directory(Spiderman.name_of_project)
        create_que_crd_files(Spiderman.name_of_project, Spiderman.homepage_url)
        Spiderman.queue = create_set(Spiderman.queue_name)
        Spiderman.crawled = create_set(Spiderman.crawled_name)

    # непосредственно функция "проползания"
    @staticmethod
    def crawling(name_of_thread, url):
        # если еще не просматривали
        if url not in Spiderman.crawled:
            # выводим в консоль основную инфу о том, что творится
            # печатаем, какой паук лазает по какому веб-сайту
            print(name_of_thread + '-spider is crawling ' + url)
            # печатаем размер очереди
            # и сколько уже прошерстили пауки
            print('\n-------\n')
            print('Queue is ' + str(len(Spiderman.queue)))
            print('Crawled is ' + str(len(Spiderman.crawled)))
            print('\n*******\n')
            Spiderman.add_to_queue(Spiderman.link_list(url))
            # удаляем из очереди проработанный url
            Spiderman.queue.remove(url)
            # и добавляем его в список проработанных
            Spiderman.crawled.add(url)
            Spiderman.converting()

    # страницы в байтах нам вернут,
    # а нам бы их в человеческий вид (HTML-страницы)
    @staticmethod
    def link_list(url):
        string = ''
        try:
            # подключаемся к веб-сайту (открываем его)
            answer = urlopen(url)
            # проверяем, что это html
            if 'text/html' in answer.getheader('Content-Type'):
                read_bytes = answer.read()
                # превращаем байты в строку
                string = read_bytes.decode("utf-8")
            finder_links = FinderLinks(Spiderman.homepage_url, url)
            finder_links.feed(string)
        # если какая-то ошибка
        except Exception as e:
            print('ОШИБКА: не могу проползти страницу!\n ->' + str(e))
            return set()
        # возвращаем множество найденных ссылок
        return finder_links.page_links()

    # функция добавления ссылок в очередь
    @staticmethod
    def add_to_queue(links):
        for link in links:
            # если её уже пробегали (или она уже
            # в очереди), то не надо добавлять в очередь
            if link in Spiderman.queue:
                continue
            if link in Spiderman.crawled:
                continue
            # чтобы не переходить в ссылки с другим доменным именем
            # типо если есть на сайте ссылка на Вк, то не надо
            # проползать весь Вк, нас интересует только то, что
            # относится к нашему домену
            # (иначе произойдёт капец, с которым я уже
            # столкнулась, и пауки попробуют обползти весь
            # Интернет)
            if Spiderman.domain_name not in link:
                continue
            Spiderman.queue.add(link)

    # Конвертируем множества обратно в файлы
    @staticmethod
    def converting():
        create_set_as_file(Spiderman.queue, Spiderman.queue_name)
        create_set_as_file(Spiderman.crawled, Spiderman.crawled_name)

