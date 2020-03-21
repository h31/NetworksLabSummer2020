# NetworksLab Summer 2020

## Параметры запуска 

### Сервер (spark)

Файл: 'src/test/java/RunServerSpark'

Параметры командной строки: путь конфигурационного файла сrawler'a, путь конфигурационного файла сервера


### Сервер (сокеты)

Файл: 'src/test/java/RunServer'

Параметры командной строки: путь конфигурационного файла сrawler'a, путь конфигурационного файла сервера

## Форматы конфигурационных файлов

### Конфигурация Crawler'a

     initialLinksFile: 
     searchDepth: 
     maxNumberOfStreams: 
     savingFolder:

 | Параметр           | Описание                                  |
 |:-------------------|:------------------------------------------|
 | initialLinksFile   | расположение файла со стартовыми ссылаки  |
 | searchDepth        | глубина сканирования сайта                |
 | maxNumberOfStreams | максимальное косичество потоков Crawler'a |
 | savingFolder       | путь сохранения файлов (для spark сервера должен быть 'src/main/resources/...')|
 
 ### Файл со стартовыми ссылками
 
    https://example.com/
    https://example.ru
    
 ### Конфигурация сервера (spark)
 
    port:
    staticFilesLocation:
    
 | Параметр            | Описание                                  |
 |:--------------------|:------------------------------------------|
 | port                | порт сервера                              |
 | staticFilesLocation | путь к сохранённым файлам ('src/main/resources/...')|
    
 ### Конфигурация сервера (сокеты)
 
    staticFilesLocation: 
    port:
    errorPageLocation: src/main/resources/static/errorPage.html
    
 | Параметр            | Описание                  |
 |:--------------------|:--------------------------|
 | staticFilesLocation | путь к сохранённым файлам |
 | port                | порт                      |
 | errorPageLocation   |  расположение html файла страницы ошибки 404 |
