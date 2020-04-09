from html.parser import HTMLParser
from urllib import parse


class FinderLinks(HTMLParser):

    def __init__(self, homepage_url, page_url):
        super().__init__()
        self.homepage_url = homepage_url
        self.page_url = page_url
        self.links = set()

    # перехватчик тега начала (перегруженный)
    def handle_starttag(self, tag, attrs):
        if tag == 'a':  # если тэг - это интересующая нас ссылка
            # нас интересует только пара href - атрибут, url - значение
            for (attribute, value) in attrs:
                if attribute == 'href':
                    # чтобы домашняя страница всегда была в url
                    # для избежания проблемы "родственных url" (relative url)
                    new_url = parse.urljoin(self.homepage_url, value)
                    self.links.add(new_url)

    # функция возвращения всех ссылок страницы
    def page_links(self):
        return self.links

    def error(self, message):
        pass
