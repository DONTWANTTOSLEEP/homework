
import requests
import pymysql
from lxml import etree
import random

header = {"User-Agent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36"}

# 入库Mysql
def save_data_sql(content):
    try:
        # 打开数据库连接-填入你Mysql的账号密码和端口
        conn = pymysql.connect(host='localhost',user='root',password='root',db='library')
        # 使用 cursor() 方法创建一个游标对象 cursor
        mycursor = conn.cursor()
        print('对应的是：',content['bookName'])
        sql = "INSERT INTO book(pk_book_name,author,amount) \
               VALUES (%s,%s,%s)"
        params = (content['bookName'],content['author'],content['amount'])
        # 调用
        mycursor.execute(sql,params)
        conn.commit()
        # 获取所有记录列表
        results = mycursor.fetchall()
        for row in results:
            print(row)
        print('成功插入', mycursor.rowcount, '条数据')
    except Exception:
        # 发生错误时回滚
        conn.rollback()
        print('发生异常')
    # 关闭数据库连接
    mycursor.close()
    conn.close()

#获取每页地址
def getUrl():
    for i in range(10):
        url = 'https://book.douban.com/top250?start={}'.format(i*25)
        urlData(url)
#获取每页数据
def urlData(url):
    html = requests.get(url,headers=header).text
    res = etree.HTML(html)
    trs = res.xpath('//*[@id="content"]/div/div[1]/div/table/tr')
    content={}
    for tr in trs:
        name = tr.xpath('./td[2]/div/a/text()')[0].strip()
        author = tr.xpath('./td[2]/p[1]/text()')[0].strip().split('/')[0]
        content["bookName"]=name
        content["author"]=author
        content["amount"]=random.randint(10,100)
        save_data_sql(content)

if __name__ == '__main__':
    getUrl()
