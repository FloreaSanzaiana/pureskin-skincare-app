from datetime import datetime

from flask import jsonify

from db_connect import *

def getQuote():
    con=get_new_connection()
    cursor = con.cursor()
    today = datetime.today().date()

    cursor.execute("SELECT * FROM daily_quote WHERE QUOTE_DATE = %s", (today,))
    existing_quote = cursor.fetchone()

    if existing_quote:
        id = existing_quote[1]
        cursor.execute("SELECT * FROM quotes WHERE ID_QUOTE = %s", (id,))
        quote = cursor.fetchone()
        cursor.close()
        con.close()
        return jsonify({"quote": str(quote[1]), "author": str(quote[2])}), 200
    else:
        cursor.execute("SELECT * FROM quotes ORDER BY RAND() LIMIT 1")
        quote = cursor.fetchone()
        cursor.execute("INSERT INTO daily_quote (ID_QUOTE, QUOTE_DATE) VALUES (%s, %s)", (quote[0], today))
        con.commit()
        cursor.close()
        con.close()
        return jsonify({"quote": str(quote[1]), "author": str(quote[2])}), 200


quotess = [

    ("Your skin is a reflection of your lifestyle, take care of it.", "Anonymous"),
    ("Beauty is being comfortable and confident in your own skin.", "Iman"),
    ("Invest in your skin. It's going to represent you for a very long time.", "Linden Tyler"),
    ("Skincare is essential. Makeup is a choice.", "Michelle Wong"),
    ("Good skincare is the foundation of great makeup.", "Anonymous"),
    ("The best thing is to look natural, but it takes makeup to look natural.", "Calvin Klein"),
    ("Happy skin is hydrated skin.", "Dr. Howard Murad"),
    ("Take good care of your skin and hydrate. If you have good skin, everything else will fall into place.",
     "Liya Kebede"),
    ("Self-care is how you take your power back.", "Lalah Delia"),
    ("Your face is a reflection of how you treat your body and soul.", "Anonymous"),
    ("Beautiful skin requires commitment, not a miracle.", "Erno Laszlo"),
    ("The skin you have today is the skin you treated yesterday.", "Anonymous"),
    ("Glowing skin comes from a healthy lifestyle and mindset.", "Anonymous"),
    ("Skincare is not vanity, it's self-care.", "Anonymous"),
    ("The most beautiful thing you can wear is healthy skin.", "Anonymous"),
    ("Skincare is healthcare. Protect your investment.", "Anonymous"),
    (
    "Your skin has a memory. In ten, twenty, thirty years from now, your skin will show the results of how it was treated today.",
    "Jana Elston"),
    ("You are what you eat, so eat beautiful.", "Sophia Loren"),
    ("Take care of your body. It's the only place you have to live.", "Jim Rohn"),
    ("The skin is the body's largest organ. Take care of it.", "Anonymous"),
    ("A good skincare routine is worth the investment.", "Anonymous"),
    ("Skin first. Makeup second. Smile always.", "Anonymous"),

    ("Healthy skin isn't made in a day. Keep going.", "Anonymous"),
    ("Beauty begins the moment you decide to be yourself.", "Coco Chanel"),
    ("The beauty of a woman is not in the clothes she wears, but the true beauty in a woman is reflected in her soul.",
     "Audrey Hepburn"),
    ("Nourish your skin from within.", "Anonymous"),
    ("Consistent skincare is key to beautiful skin.", "Anonymous"),
    ("The best foundation you can wear is healthy glowing skin.", "Anonymous"),
    ("Treat your skin like silk, not sandpaper.", "Anonymous"),
    ("Hydrated skin is happy skin.", "Anonymous"),
    ("Beauty is power; a smile is its sword.", "John Ray"),
    ("Caring for your skin is caring for your overall health.", "Anonymous"),
    ("Water is the essence of moisture, and moisture is the essence of beauty.", "Anonymous"),
    ("Your skin deserves the same level of care as your wardrobe.", "Anonymous"),
    ("Skincare isn't about being perfect; it's about being better to yourself.", "Anonymous"),
    ("Radiant skin comes from inner peace.", "Anonymous"),
    ("Just like a plant needs water to grow, your skin needs hydration to glow.", "Anonymous"),
    ("Make time for self-care; your skin will thank you later.", "Anonymous"),
    ("Beauty is being the best possible version of yourself, inside and out.", "Audrey Hepburn"),
    ("The science of skincare is as important as the products you use.", "Anonymous"),
    ("True beauty comes from taking care of your overall health.", "Anonymous"),
    ("Skincare is a journey, not a destination.", "Anonymous"),
    ("Let your skin breathe; less is more.", "Anonymous"),
    ("Beautiful skin begins with exceptional skincare.", "Anonymous"),
    ("Self-love is the greatest skincare routine.", "Anonymous"),
    ("Protection today, prevention tomorrow.", "Anonymous"),
    ("Confidence is the most beautiful makeup.", "Anonymous"),
    ("Skincare is for everyone, regardless of age or gender.", "Anonymous"),
    ("Your skin tells your story; make it a good one.", "Anonymous")
]


def insertQuotes():
    cursor = conn.cursor()
    for quote, author in quotess:
        cursor.execute(f"INSERT INTO QUOTES (QUOTE, AUTOR) VALUES(\"{quote}\", \"{author}\");")

    conn.commit()

if __name__=="__main__":
    insertQuotes()
