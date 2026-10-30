import { useEffect, useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../App.jsx'
import { api } from '../api.js'
import { iniciais } from '../util.js'
import { Alert, Field, Modal } from './ui.jsx'

const NOME_PERFIL = { GESTOR: 'Gestão / Secretaria', PROFESSOR: 'Professor(a)', ALUNO: 'Aluno(a)', RESPONSAVEL: 'Responsável' }

const SENHA_VAZIA = { atual: '', nova: '', conf: '' }

export default function Layout({ menu }) {
  const { usuario, sair } = useAuth()
  const [aberto, setAberto] = useState(false)
  const [trocando, setTrocando] = useState(false)
  const [senha, setSenha] = useState(SENHA_VAZIA)
  const [msg, setMsg] = useState({})
  const [enviando, setEnviando] = useState(false)
  const local = useLocation()
  useEffect(() => setAberto(false), [local.pathname])
  const atual = menu.find((m) => m.to === local.pathname)

  const abrirSenha = () => { setSenha(SENHA_VAZIA); setMsg({}); setTrocando(true) }

  const trocarSenha = async () => {
    setMsg({})
    if (senha.nova.length < 6) { setMsg({ erro: 'A nova senha deve ter ao menos 6 caracteres.' }); return }
    if (senha.nova !== senha.conf) { setMsg({ erro: 'A confirmação não confere com a nova senha.' }); return }
    setEnviando(true)
    try {
      await api.post('/auth/senha', { senhaAtual: senha.atual, novaSenha: senha.nova })
      setMsg({ ok: 'Senha alterada com sucesso.' })
      setSenha(SENHA_VAZIA)
    } catch (e) { setMsg({ erro: e.message }) } finally { setEnviando(false) }
  }

  return (
    <div className="app">
      <aside className={`sidebar ${aberto ? 'open' : ''}`}>
        <div className="brand">
          <span className="brand-logo">🌱</span>
          <div>
            <strong>Semeando Digital</strong>
            <small>Gestão Escolar</small>
          </div>
        </div>
        <nav>
          {menu.map((m, i) => (
            <NavLink key={m.to} to={m.to} end={i === 0} className="nav-item">
              <span className="nav-icon">{m.icon}</span>{m.label}
            </NavLink>
          ))}
        </nav>
        <div className="side-user">
          <div className="avatar">{iniciais(usuario.nome)}</div>
          <div className="side-user-info">
            <strong>{usuario.nome}</strong>
            <small>{NOME_PERFIL[usuario.perfil]}</small>
          </div>
        </div>
        <div className="side-actions">
          <button className="btn btn-ghost btn-block" onClick={abrirSenha}>🔑 Trocar senha</button>
          <button className="btn btn-ghost btn-block" onClick={sair}>Sair</button>
        </div>
      </aside>
      {aberto && <div className="backdrop" onClick={() => setAberto(false)} />}
      <div className="main">
        <header className="topbar">
          <button className="icon-btn menu-btn" onClick={() => setAberto(true)} aria-label="Abrir menu">☰</button>
          <h1>{atual?.label}</h1>
          <span className="chip">{NOME_PERFIL[usuario.perfil]}</span>
        </header>
        <main className="content">
          <Outlet />
        </main>
      </div>

      {trocando && (
        <Modal title="Trocar senha" onClose={() => setTrocando(false)} footer={<>
          <button className="btn btn-ghost" onClick={() => setTrocando(false)}>Fechar</button>
          <button className="btn btn-primary" onClick={trocarSenha} disabled={enviando}>{enviando ? 'Salvando...' : 'Salvar nova senha'}</button>
        </>}>
          <Alert tone="error">{msg.erro}</Alert>
          <Alert tone="success">{msg.ok}</Alert>
          <Field label="Senha atual" full>
            <input type="password" autoComplete="current-password" value={senha.atual} onChange={(e) => setSenha({ ...senha, atual: e.target.value })} />
          </Field>
          <Field label="Nova senha (mínimo 6 caracteres)" full>
            <input type="password" autoComplete="new-password" value={senha.nova} onChange={(e) => setSenha({ ...senha, nova: e.target.value })} />
          </Field>
          <Field label="Confirmar nova senha" full>
            <input type="password" autoComplete="new-password" value={senha.conf} onChange={(e) => setSenha({ ...senha, conf: e.target.value })} />
          </Field>
        </Modal>
      )}
    </div>
  )
}
