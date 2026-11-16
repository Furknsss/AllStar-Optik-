"""Offline integrity check for the bundled answer key (Python 3 standard library)."""
from pathlib import Path
import sqlite3
path=Path(__file__).resolve().parents[1]/'app/src/main/assets/answers.sqlite'
c=sqlite3.connect(path)
assert c.execute('PRAGMA integrity_check').fetchone()[0]=='ok'
tests=c.execute('SELECT COUNT(*) FROM tests').fetchone()[0]
answers=c.execute('SELECT COUNT(*) FROM answers').fetchone()[0]
mismatched=c.execute('SELECT COUNT(*) FROM tests t WHERE t.answer_entries!=(SELECT COUNT(*) FROM answers a WHERE a.test_id=t.id)').fetchone()[0]
problematic=c.execute('SELECT COUNT(*) FROM tests WHERE needs_review=1').fetchone()[0]
missinglabels=c.execute("SELECT COUNT(*) FROM tests WHERE test_label_extracted IS NULL OR trim(test_label_extracted) = ''").fetchone()[0]
invalid_letters=c.execute("SELECT COUNT(*) FROM answers WHERE correct_option NOT IN ('A','B','C','D','E')").fetchone()[0]
print(f'{tests} test grubu, {answers} cevap, {problematic} numara sorunu, {missinglabels} belirsiz etiket, {mismatched} sayım hatası, {invalid_letters} geçersiz şık')
assert tests==2564 and answers==22673 and problematic==3 and missinglabels==0 and mismatched==0 and invalid_letters==0
print('Veri tutarlılığı: OK (kaynak doğruluk sertifikası değildir)')

active=c.execute('SELECT COUNT(*) FROM tests WHERE needs_review=0').fetchone()[0]
assert active==2561
assert c.execute('SELECT COUNT(*) FROM tests WHERE needs_review=0 AND (test_label_extracted IS NULL OR trim(test_label_extracted)="")').fetchone()[0]==0
for id,n in c.execute('SELECT id,answer_entries FROM tests WHERE needs_review=0').fetchall():
    x=c.execute('SELECT printed_question_no,correct_option FROM answers WHERE test_id=? ORDER BY entry_index',(id,)).fetchall()
    assert len(x)==n and all(no==i+1 and option in 'ABCDE' for i,(no,option) in enumerate(x)),id
print('Aktif 2561 testin soru numarası ve etiketi doğrulandı; basılı hatalı 3 kayıt kapalı')
