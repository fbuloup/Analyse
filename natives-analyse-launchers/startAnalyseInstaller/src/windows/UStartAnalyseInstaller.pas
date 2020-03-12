unit UStartAnalyseInstaller;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, StdCtrls, ShellApi;

type
  TStartAnalyseInstallerForm = class(TForm)
    Label1: TLabel;
    Label2: TLabel;
    Button1: TButton;
    Label3: TLabel;
    Label4: TLabel;
    procedure Label3Click(Sender: TObject);
    procedure Label3MouseEnter(Sender: TObject);
    procedure Label3MouseLeave(Sender: TObject);
    procedure Button1Click(Sender: TObject);
    procedure FormShow(Sender: TObject);
  private
    { Déclarations privées }
  public
    { Déclarations publiques }
  end;

var
  StartAnalyseInstallerForm: TStartAnalyseInstallerForm;
  errorNum : Cardinal;

implementation

{$R *.dfm}

procedure TStartAnalyseInstallerForm.Label3Click(Sender: TObject);
var URL : String;
begin
  URL := 'http://www.java.com/fr/download/manual.jsp';
  ShellExecute(GetDesktopWindow(), 'open',PChar(URL), nil, nil, SW_SHOWNORMAL);
end;

procedure TStartAnalyseInstallerForm.Label3MouseEnter(Sender: TObject);
begin
  (Sender as TLabel).Font.Style := [fsUnderline];
end;

procedure TStartAnalyseInstallerForm.Label3MouseLeave(Sender: TObject);
begin
  (Sender as TLabel).Font.Style := [];
end;

procedure TStartAnalyseInstallerForm.Button1Click(Sender: TObject);
begin
  Application.Terminate;
end;

procedure TStartAnalyseInstallerForm.FormShow(Sender: TObject);
begin
  if(errorNum <> 0) then
  begin
    Label1.Visible := false;
    Label2.Visible := false;
    Label3.Visible := false;
    Label4.Visible := true;
    If(errorNum = ERROR_FILE_NOT_FOUND) then Label4.Caption := 'ERROR_FILE_NOT_FOUND';
    If(errorNum = ERROR_PATH_NOT_FOUND) then Label4.Caption := 'ERROR_PATH_NOT_FOUND';
    if(errorNum = ERROR_DDE_FAIL) then Label4.Caption := 'ERROR_DDE_FAIL';
    if(errorNum = ERROR_NO_ASSOCIATION) then Label4.Caption := 'ERROR_NO_ASSOCIATION';
    if(errorNum = ERROR_ACCESS_DENIED) then Label4.Caption := 'ERROR_ACCESS_DENIED';
    if(errorNum = ERROR_DLL_NOT_FOUND) then Label4.Caption := 'ERROR_DLL_NOT_FOUND';
    if(errorNum = ERROR_CANCELLED) then Label4.Caption := 'ERROR_CANCELLED';
    if(errorNum = ERROR_NOT_ENOUGH_MEMORY) then Label4.Caption := 'ERROR_NOT_ENOUGH_MEMORY';
    if(errorNum = ERROR_SHARING_VIOLATION) then Label4.Caption := 'ERROR_SHARING_VIOLATION';
    if(errorNum = ERROR_CLASS_DOES_NOT_EXIST) then Label4.Caption := 'ERROR_CLASS_DOES_NOT_EXIST';
  end;
end;

end.
