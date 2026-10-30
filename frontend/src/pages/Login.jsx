import { useState } from 'react'
import { useAuth } from '../App.jsx'
import { Alert } from '../components/ui.jsx'

export default function Login() {
  const { entrar } = useAuth()
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [verSenha, setVerSenha] = useState(false)
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  const enviar = async (e) => {
    e?.preventDefault()
    setErro('')
    setEnviando(true)
    try { await entrar(email.trim(), senha) } catch (err) { setErro(err.message) } finally { setEnviando(false) }
  }

  return (
    <div className="login-page">
      <div className="login-hero">
        <div className="login-hero-inner">
          <span className="hero-logo">🌱</span>
          <h1>Semeando Digital</h1>
          <p>Sistema de Gestão Escolar do Centro Educacional Semeando — matrícula, contratos, cobrança, diário de classe e portal da família em um só lugar.</p>
          <ul>
            <li>✔ Cobrança dual: taxa de associado e mensalidade</li>
            <li>✔ Validação automática contra a tabela oficial</li>
            <li>✔ Checklist de documentos e dados de emergência</li>
            <li>✔ Auditoria e conformidade com a LGPD</li>
          </ul>
        </div>
      </div>
      <div className="login-box">
        <form className="login-form" onSubmit={enviar}>
          <h2>Entrar</h2>
          <p className="muted">Acesse com o e-mail e a senha fornecidos pela secretaria.</p>
          <Alert tone="error">{erro}</Alert>
          <label className="field">
            <span>E-mail</span>
            <input id="email" type="email" autoComplete="username" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="seu@email.com" required autoFocus />
          </label>
          <label className="field">
            <span>Senha</span>
            <div className="senha-wrap">
              <input id="senha" type={verSenha ? 'text' : 'password'} value={senha} onChange={(e) => setSenha(e.target.value)}
                placeholder="••••••" autoComplete="current-password" required />
              <button type="button" className="btn-link senha-toggle" onClick={() => setVerSenha(!verSenha)}>{verSenha ? 'Ocultar' : 'Mostrar'}</button>
            </div>
          </label>
          <button className="btn btn-primary btn-block" disabled={enviando}>{enviando ? 'Entrando...' : 'Entrar'}</button>
          <p className="muted small lgpd">🔒 Ambiente de demonstração com dados 100% fictícios (LGPD, art. 14).</p>
        </form>
      </div>
    </div>
  )
}
