from urllib.parse import urlparse


# Получим укороченное доменное имя (something.ru)
def get_little_domain(url):
    try:
        # делим полученное доменное имя по частям через точку
        res = get_domain_name(url).split('.')
        # берем последние 2 элемента и разделяем их точкой
        return res[-2] + '.' + res[-1]
    except Exception as e:
        print(str(e))
        return ''


# Функция извлечения доменного имени из url домашней страницы
def get_domain_name(url):
    try:
        return urlparse(url).netloc
    except Exception as e:
        print(str(e))
        return ''
