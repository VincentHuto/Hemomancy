import sys
from PIL import Image
OUT=sys.argv[1]; PREVIEW=sys.argv[2]
def hx(s): s=s.lstrip('#'); return tuple(int(s[i:i+2],16) for i in (0,2,4))+(255,)
COMMON={
 'o':'#170f12',              # outline
 # hematic iron
 'D':'#34303a','M':'#565260','L':'#7e7a88','H':'#aaa6b2',
 # blood
 'r':'#5c0c14','R':'#9e1a24','E':'#d4363c',
 # socket (staff collar, matches the staff's venous stone)
 's':'#4a2226','S':'#7a4448','T':'#a06a6a',
}
SPRITES={}
SPRITES['worn_vow_fitting']=({},[
"................",
".......oo.......",
"......oHMo......",
"...oo.oLDo.oo...",
"..oHMooLDooLDo..",
"..oLDoLMMDoMDo..",
"..oooooooooooo..",
"..oHLLLLLLLMDo..",
"..oLMRRRRRRMDo..",
"..oLMMMRRMMMDo..",
"..oLMMMRRMMMDo..",
"..oLMMMrrMMDDo..",
"..ooDDDDDDDDoo..",
"....oooooooo....",
"......oTSo......",
"......oSso......"])
SPRITES['barbed_fitting']=({'P':'#f2b8bc','O':'#e07a3a','Q':'#ffb070','c':'#b82a44','C':'#d84a5e'},[
".......oo.......",
".......oPo......",
"...oo..oCo..oo..",
"..oPo.oCcRo.oPo.",
"..oCRooCcRooCRo.",
".oPoCRoCcRoCRoPo",
".oCRoCRCRRCRoCRo",
"..oCRoCOQRoRCRo.",
"..oCcRoOQRoCcRo.",
"...oCcRoOooCRo..",
"...oCcCcRCcRro..",
"....oRcRRRRro...",
".....oorrrroo...",
"......oTSo......",
"......oSso......",
"................"])
SPRITES['chitinite_fitting']=({'D':'#2c3448','M':'#4c5a76','L':'#7486a6','H':'#a6b6d0','G':'#3fb8c0','J':'#9ff4f0','K':'#1e6e78'},[
"......oooo......",
".....oJJGGo.....",
"..o..oJGGKo..o..",
".oHo..oKKo..oMo.",
".oLo...oo...oDo.",
".oLMo..oo..oMDo.",
"..oLo.oLDo.oMo..",
"..oLMooLDooMDo..",
"...oLMMLMMMDo...",
"....oLLLMMDo....",
"....oooooooo....",
"....oHLLMMDo....",
".....oooooo.....",
"......oTSo......",
"......oSso......",
"................"])
SPRITES['prismatic_fitting']=({'a':'#2a1032','b':'#40184c','d':'#5e2a6c','P':'#ff9cf4','Q':'#e050e0','V':'#a020b0','W':'#fff0ff'},[
".......oo.......",
"......oWQo......",
"......oPQo...o..",
"...o..oPVo..oQo.",
"..oWo.oPVo.oPVo.",
"..oPQooPVooPVo..",
"..oPVoWPQVoPVo..",
".ooPVoPQVVoQVoo.",
".oWooooooooooWo.",
".odbbbbbbbbbbao.",
".odbbbbbbbbbbao.",
"..odbbbbbbbbao..",
"...ooaaaaaaoo...",
"......oTSo......",
"......oSso......",
"................"])
SPRITES['crimson_vestment_fitting']=({'g':'#3a3a44','G':'#62626e','F':'#8c8c98','B':'#efe3c8','b':'#c0ae8e','k':'#24161a'},[
".....oooooo.....",
".....oFGGgoo....",
".....oGGGgoRo...",
".....oRRRrooo...",
"..oooooooooooo..",
"...oooooooooo...",
"....oBBBBbbo....",
"....oBkBBkbo....",
"....oBBBBBbo....",
"....oBBkkbbo....",
"....oRbbbbro....",
"...oERRRRRRro...",
"...oRRrrrrrro...",
"....oooooooo....",
"......oTSo......",
"......oSso......"])
SPRITES['monolithic_frame_fitting']=({'g':'#6e6878','F':'#4a4652','f':'#2e2a34','m':'#1e1418','n':'#3e2c34'},[
".oooooooooooooo.",
".oRggggggggggRo.",
".oFFFFFFFFFFFfo.",
".oFoooooooooofo.",
".oFo..oooo..ofo.",
".oFo.onmmno.ofo.",
".oFo.oRERro.ofo.",
".oFo.onmmno.ofo.",
".oFo.oRERro.ofo.",
".oFo.onmmno.ofo.",
".oFo..oooo..ofo.",
".oFoooooooooofo.",
".oRFFFFFFFFFFRo.",
".oooooooooooooo.",
"......oTSo......",
"......oSso......"])
SPRITES['assumed_limb_fitting']=({'B':'#f0e2c4','b':'#c8b28c','d':'#8e7858','w':'#fff8e8'},[
"................",
"....o.oo.o......",
"...oEoEEoEo.....",
"...oBoBRoBo.o...",
"...oBoBwoBooEo..",
"...oBoBboBooBo..",
"...oboBboboBbo..",
"...oBBBBBBbbo...",
"..oRRRRRRRRRro..",
"...owBBBBBbbo...",
"....oobbbbdo....",
".....oRrRro.....",
".....owBbdo.....",
".....oRrRro.....",
"......oTSo......",
"......oSso......"])
tiles=[]
for name,(extra,rows) in SPRITES.items():
    pal=dict(COMMON); pal.update(extra)
    assert len(rows)==16 and all(len(r)==16 for r in rows),(name,[len(r) for r in rows])
    im=Image.new('RGBA',(16,16),(0,0,0,0))
    for y,row in enumerate(rows):
        for x,ch in enumerate(row):
            if ch!='.': im.putpixel((x,y),hx(pal[ch]))
    if OUT!='-': im.save(f'{OUT}/{name}.png')
    tiles.append(im)
W=192
sheet=Image.new('RGBA',((W+10)*len(tiles)+10,W+20+48),(139,139,139,255))
for k,t in enumerate(tiles):
    r=t.resize((W,W),Image.NEAREST); sheet.paste(r,(10+k*(W+10),10),r)
    s=t.resize((48,48),Image.NEAREST); sheet.paste(s,(10+k*(W+10),W+15),s)
    sheet.paste(t,(70+k*(W+10),W+30),t)
sheet.save(PREVIEW)
