import os

# Каждая веб-страница, которую буду обползать пауком будет являться
# самостоятельной единиццей (проектом)


# Функция создания директории для проекта (только если такой нет)
def create_directory(directory):
    # проверяю, что нет такой
    if not os.path.exists(directory):
        print('Создание проекта для ' + directory)
        os.makedirs(directory)


# Функция создания файла на запись (по пути и данным)
def writing_fun(filepath, data):
    with open(filepath, 'w') as file:  # открытие файла на запись
        file.write(data)  # запись данных в файл


# Функция создания 2 текстовых файлов: очередь и уже "пойманные" файлы
# homepage - url домашней страницы веб-сайта
def create_que_crd_files(name, homepage):
    # имя файла с листом ожидания
    queue = os.path.join(name, 'queue.txt')
    # имя файла с пойманными
    crawled = os.path.join(name, 'crawled.txt')
    # создаём их только если их еще не существует
    if not os.path.isfile(queue):
        writing_fun(queue, homepage)
    if not os.path.isfile(crawled):
        writing_fun(crawled, '')


# Функция для дописывания инфы в уже сужествующий файл
def add_data_to_file(filepath, data):
    with open(filepath, 'a') as file:  # а - это флаг "дозаписывания"
        file.write(data + '\n')


# Функция удаления содержимого файла
def kill_content(filepath):
    open(filepath, 'w').close()  # открытие файла на запись (всё перезапишется)


# Буду использовать Set, чтобы не было повторов (одинаковых записей)
# Конвертирую каждую строку файла в множество (Set)
def create_set(name):
    res = set()  # создаю пустое множество
    with open(name, 'rt') as f:
        for line in f:  # добавляем во множество каждую строку
            # при этом надо не забыть убрать символ переноса строки,
            # он добавлен мной ранее для лучшей читабельности
            res.add(line.replace('\n', ''))
        return res


# Конвертируем множество (уже без повторов) - обратно в файл
def create_set_as_file(lines, file):
    with open(file, 'w') as f:
        # для каждой строки (из отсортированных) делаем дописывание в файл
        for line in sorted(lines):
            f.write(line + '\n')
